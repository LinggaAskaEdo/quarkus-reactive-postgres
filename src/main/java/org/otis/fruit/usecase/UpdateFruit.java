package org.otis.fruit.usecase;

import java.util.UUID;

import org.otis.fruit.domain.FruitRepository;
import org.otis.shared.constant.StatusMsgEnum;
import org.otis.shared.dto.DtoResponse;
import org.otis.shared.util.DtoHelper;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UpdateFruit {
	private final FruitRepository fruitRepository;

	public UpdateFruit(FruitRepository fruitRepository) {
		this.fruitRepository = fruitRepository;
	}

	public Uni<DtoResponse> execute(UUID id, String name) {
		return fruitRepository.update(id, name).onItem().ifNotNull()
				.transform(resp -> DtoHelper.constructResponse(StatusMsgEnum.SUCCESS,
						"Data updated successfully !!!", resp))
				.onItem().ifNull()
				.continueWith(() -> DtoHelper.constructResponse(StatusMsgEnum.FAILED,
						"Data update failed !!!", null))
				.onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED,
						failure.getMessage(), null));
	}
}
