package org.otis.fruit.usecase;

import java.util.UUID;

import org.otis.fruit.domain.FruitRepository;
import org.otis.shared.constant.StatusMsgEnum;
import org.otis.shared.dto.DtoResponse;
import org.otis.shared.util.DtoHelper;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeleteFruit {
	private final FruitRepository fruitRepository;

	public DeleteFruit(FruitRepository fruitRepository) {
		this.fruitRepository = fruitRepository;
	}

	public Uni<DtoResponse> execute(UUID id) {
		return fruitRepository.deleteById(id).onItem()
				.transform(deleted -> Boolean.TRUE.equals(deleted)
						? DtoHelper.constructResponse(StatusMsgEnum.SUCCESS,
								"Data deleted successfully !!!", null)
						: DtoHelper.constructResponse(StatusMsgEnum.FAILED,
								"Data delete failed !!!", null))
				.onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED,
						failure.getMessage(), null));
	}
}
